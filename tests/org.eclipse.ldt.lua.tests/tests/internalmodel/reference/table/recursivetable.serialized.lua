do local _={
		unknownglobalvars={

		}
		--[[table: 0x97bdc20]],
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
											expression={
												sourcerange={
													min=45,
													max=53
												}
												--[[table: 0x988ad60]],
												definition=nil --[[ref]],
												tag="MIdentifier"
											}
											--[[table: 0x988acc0]],
											returnposition=1,
											tag="exprtyperef"
										}
										--[[table: 0x98daad0]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=33,
											max=41
										}
										--[[table: 0x98dabc0]],
										occurrences={

										}
										--[[table: 0x98dab98]],
										tag="item"
									}
									--[[table: 0x98dab70]]
								}
								--[[table: 0x9469850]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x9469878]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x9853000]],
							tag="inlinetyperef"
						}
						--[[table: 0x94698a0]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x96be4e8]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x943a840]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x943a818]],
							[2]={
								sourcerange={
									min=23,
									max=31
								}
								--[[table: 0x956cab0]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x956ca10]],
							[3]=nil --[[ref]]
						}
						--[[table: 0x96be4c0]],
						tag="item"
					}
					--[[table: 0x96be498]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x97d7120]]
				}
				--[[table: 0x97d70b8]]
			}
			--[[table: 0x94e0598]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x94e05c0]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=23,
						max=41
					}
					--[[table: 0x94e0840]],
					right="fieldname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x956cad8]],
				[3]=nil --[[ref]]
			}
			--[[table: 0x97bdce8]],
			tag="MBlock"
		}
		--[[table: 0x97bdc48]],
		tag="MInternalContent"
	}
	--[[table: 0x94e0570]];
	_.content.localvars[1].item.type.def.fields.fieldname.type.expression.definition=_.content.localvars[1].item;
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[3]=_.content.localvars[1].item.type.def.fields.fieldname.type.expression;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	_.content.content[3]=_.content.localvars[1].item.type.def.fields.fieldname.type.expression;
	return _;
end