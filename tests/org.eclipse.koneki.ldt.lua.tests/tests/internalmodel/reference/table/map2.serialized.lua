do local _={
		unknownglobalvars={

		}
		--[[table: 0x978b9e8]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="table",
								fields={
									fieldname={
										type={
											typename="number",
											tag="primitivetyperef"
										}
										--[[table: 0x98539c0]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=33,
											max=43
										}
										--[[table: 0x9853c40]],
										occurrences={

										}
										--[[table: 0x9853c18]],
										tag="item"
									}
									--[[table: 0x9853a28]]
								}
								--[[table: 0x9853848]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x9853870]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x955d118]],
							tag="inlinetyperef"
						}
						--[[table: 0x98538d8]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x955d070]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9549260]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x98e8aa8]],
							[2]={
								sourcerange={
									min=23,
									max=31
								}
								--[[table: 0x9813cf8]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x9549170]]
						}
						--[[table: 0x955d048]],
						tag="item"
					}
					--[[table: 0x955ce58]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x98ac108]]
				}
				--[[table: 0x98ac0a0]]
			}
			--[[table: 0x978bad8]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x978bb00]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=23,
						max=44
					}
					--[[table: 0x9813dc0]],
					right="fieldname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x9813d20]]
			}
			--[[table: 0x978bab0]],
			tag="MBlock"
		}
		--[[table: 0x978ba10]],
		tag="MInternalContent"
	}
	--[[table: 0x978b948]];
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	return _;
end