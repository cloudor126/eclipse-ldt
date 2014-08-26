do local _={
		unknownglobalvars={

		}
		--[[table: 0x94123a8]],
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
											def={
												shortdescription="",
												name="table",
												fields={
													secondfield={
														type={
															typename="number",
															tag="primitivetyperef"
														}
														--[[table: 0x98b7e90]],
														description="",
														parent=nil --[[ref]],
														shortdescription="",
														name="secondfield",
														sourcerange={
															min=56,
															max=66
														}
														--[[table: 0x964f850]],
														occurrences={

														}
														--[[table: 0x964f828]],
														tag="item"
													}
													--[[table: 0x964f7a0]]
												}
												--[[table: 0x9877298]],
												sourcerange={
													min=0,
													max=0
												}
												--[[table: 0x95aa128]],
												description="",
												tag="recordtypedef"
											}
											--[[table: 0x9877230]],
											tag="inlinetyperef"
										}
										--[[table: 0x95aa190]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="fieldname",
										sourcerange={
											min=21,
											max=29
										}
										--[[table: 0x982f7b0]],
										occurrences={

										}
										--[[table: 0x982f788]],
										tag="item"
									}
									--[[table: 0x982f700]]
								}
								--[[table: 0x98771e0]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x9877208]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x98771b8]],
							tag="inlinetyperef"
						}
						--[[table: 0x982f7d8]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x9877130]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x9403860]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x94037e0]],
							[2]={
								sourcerange={
									min=36,
									max=44
								}
								--[[table: 0x97d66d0]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x96f3628]]
						}
						--[[table: 0x9647e20]],
						tag="item"
					}
					--[[table: 0x96cd5a0]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x964f8a0]]
				}
				--[[table: 0x964f878]]
			}
			--[[table: 0x94124c0]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x94124e8]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=36,
						max=66
					}
					--[[table: 0x97d67d0]],
					right="secondfield",
					left={
						sourcerange={
							min=36,
							max=54
						}
						--[[table: 0x97d6780]],
						right="fieldname",
						left=nil --[[ref]],
						tag="MIndex"
					}
					--[[table: 0x97d6758]],
					tag="MIndex"
				}
				--[[table: 0x97d67a8]]
			}
			--[[table: 0x9412498]],
			tag="MBlock"
		}
		--[[table: 0x9412410]],
		tag="MInternalContent"
	}
	--[[table: 0x93ce820]];
	_.content.localvars[1].item.type.def.fields.fieldname.type.def.fields.secondfield.parent=_.content.localvars[1].item.type.def.fields.fieldname.type.def;
	_.content.localvars[1].item.type.def.fields.fieldname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left.left=_.content.localvars[1].item.occurrences[2];
	return _;
end